import React, { Dispatch, SetStateAction } from "react";
import {
  Button,
  Dialog,
  Grid,
  makeStyles,
  TextField,
  Typography
} from "@material-ui/core";
import { useFormik } from "formik";
import { BotIndexResponse } from "../../../utils/types/bots";
import * as Yup from "yup";
import { api } from "../../../utils/Api";

type Props = {
  selectedBot?: BotIndexResponse["data"][number];
  modalOpen: boolean;
  setModalOpen: Dispatch<SetStateAction<boolean>>;
  setSelectedBot: Dispatch<
    SetStateAction<BotIndexResponse["data"][number] | undefined>
  >;
  setBotList: Dispatch<BotIndexResponse["data"]>;
};

type FormValues = {
  id: string;
  clientId: string;
  clientSecret: string;
};

const useStyles = makeStyles({
  form: {
    padding: "0 8px"
  },
  formGroup: {
    padding: 20
  }
});

export const UpdateBotFormModal: React.FC<Props> = ({
  selectedBot,
  modalOpen,
  setModalOpen,
  setSelectedBot,
  setBotList
}) => {
  const classes = useStyles();

  const closeModal = () => {
    setModalOpen(false);
    setSelectedBot(undefined);
  };

  const formik = useFormik<FormValues>({
    initialValues: {
      id: "",
      clientId: "",
      clientSecret: ""
    },
    validationSchema: Yup.object({
      id: selectedBot
        ? Yup.string().min(1, "1文字以上を入力してください")
        : Yup.string()
            .min(1, "1文字以上を入力してください")
            .required("必須です")
    }),
    onSubmit: (values, submitProps) => {
      const param = {
        ...values,
        id: selectedBot?.id || values.id,
        clientId: values.clientId === "" ? null : values.clientId,
        clientSecret: values.clientSecret === "" ? null : values.clientSecret
      };

      api
        .post(
          `http://localhost:9000/bots/${selectedBot?.id || values.id}`,
          param
        )
        .then(() => {
          submitProps.resetForm();
          closeModal();
          api
            .get<BotIndexResponse>("http://localhost:9000/bots")
            .then(res => setBotList(res.data.data))
            .catch(e => alert(e));
        })
        .catch(e => {
          submitProps.resetForm();
          alert(e);
        });
    }
  });

  return (
    <Dialog
      maxWidth={"lg"}
      fullWidth
      open={modalOpen}
      onBackdropClick={() => closeModal()}
    >
      <form onSubmit={formik.handleSubmit} className={classes.formGroup}>
        <Grid
          container
          justify={"space-between"}
          direction={"row"}
          alignItems={"center"}
        >
          <Grid item>
            <Grid container direction={"row"}>
              <Grid item className={classes.form}>
                <TextField
                  label={"BotID"}
                  name={"id"}
                  value={selectedBot ? selectedBot.id : formik.values.id}
                  onChange={formik.handleChange}
                  disabled={!!selectedBot}
                  variant={"outlined"}
                />
                {formik.touched.id && formik.errors.id && (
                  <Typography variant={"subtitle2"} color={"error"}>
                    {formik.errors.id}
                  </Typography>
                )}
              </Grid>
              <Grid item className={classes.form}>
                <TextField
                  label={"clientID"}
                  name={"clientId"}
                  value={formik.values.clientId}
                  onChange={formik.handleChange}
                  variant={"outlined"}
                />
                {formik.touched.clientId && formik.errors.clientId && (
                  <Typography variant={"subtitle2"} color={"error"}>
                    {formik.errors.clientId}
                  </Typography>
                )}
              </Grid>
              <Grid item className={classes.form}>
                <TextField
                  label={"clientSecret"}
                  name={"clientSecret"}
                  value={formik.values.clientSecret}
                  onChange={formik.handleChange}
                  variant={"outlined"}
                />
                {formik.touched.clientSecret && formik.errors.clientSecret && (
                  <Typography variant={"subtitle2"} color={"error"}>
                    {formik.errors.clientSecret}
                  </Typography>
                )}
              </Grid>
            </Grid>
          </Grid>
          <Grid item>
            <Button
              type={"submit"}
              disabled={formik.isSubmitting}
              color={"primary"}
              variant={"outlined"}
            >
              登録
            </Button>
          </Grid>
        </Grid>
      </form>
    </Dialog>
  );
};
