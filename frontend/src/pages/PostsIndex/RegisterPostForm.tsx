// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { useFormik } from "formik";
import React, { Dispatch, SetStateAction, useEffect, useState } from "react";
import {
  Button,
  Chip,
  FormControl,
  Grid,
  Input,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  TextField,
  Typography
} from "@material-ui/core";
import { format, getUnixTime, startOfToday } from "date-fns";
import { api } from "../../utils/Api";
import * as Yup from "yup";
import { BotIndexResponse } from "../../utils/types/bots";
import { PostsIndexResponse } from "../../utils/types/posts";

type FormValues = {
  url: string;
  title: string;
  author: string;
  postedAt: string;
  botIds: string[];
};

type Props = {
  setPosts: Dispatch<SetStateAction<PostsIndexResponse["data"]>>;
};

const RegisterPostForm: React.FC<Props> = ({ setPosts }) => {
  const [botList, setBotList] = useState<BotIndexResponse["data"]>([]);

  const formik = useFormik<FormValues>({
    initialValues: {
      url: "",
      title: "",
      author: "",
      postedAt: format(startOfToday(), "yyyy-MM-dd"),
      botIds: []
    },
    validationSchema: Yup.object({
      url: Yup.string()
        .min(1, "1文字以上を入力してください")
        .required("必須です"),
      title: Yup.string()
        .min(1, "1文字以上を入力してください")
        .required("必須です"),
      author: Yup.string()
        .min(1, "1文字以上を入力してください")
        .required("必須です"),
      postedAt: Yup.string().required("必須です"),
      botIds: Yup.array().min(1, "1つ以上選択してください")
    }),
    onSubmit: values => {
      const req = {
        ...values,
        postedAt: getUnixTime(new Date(values.postedAt))
      };
      api
        .post("http://localhost:9000/posts", req)
        .then(() =>
          api
            .get<PostsIndexResponse>("http://localhost:9000/posts")
            .then(r => setPosts(r.data.data))
        );
    }
  });

  useEffect(() => {
    api.get<BotIndexResponse>("http://localhost:9000/bots").then(v => {
      setBotList(v.data.data);
    });
  }, []);

  return (
    <Paper style={{ width: "90%", margin: "auto" }}>
      <form onSubmit={formik.handleSubmit}>
        <Grid
          container
          direction={"row"}
          alignItems={"center"}
          style={{ padding: "12px" }}
          justify={"space-between"}
        >
          <Grid item style={{ width: "100%" }}>
            <TextField
              fullWidth
              label={"URL"}
              name={"url"}
              type={"url"}
              value={formik.values.url}
              onChange={formik.handleChange}
            />
            {formik.touched.url && formik.errors.url && (
              <Typography variant={"subtitle2"} color={"error"}>
                {formik.errors.url}
              </Typography>
            )}
          </Grid>
          <Grid item style={{ width: "100%" }}>
            <FormControl fullWidth>
              <InputLabel>カテゴリー</InputLabel>
              <Select
                multiple
                value={formik.values.botIds}
                input={<Input />}
                onChange={formik.handleChange}
                name={"botIds"}
                renderValue={selected => (
                  <Grid container direction={"row"}>
                    {(selected as string[]).map((value, index) => (
                      <Grid item key={value}>
                        <Chip label={botList[index].name} />
                      </Grid>
                    ))}
                  </Grid>
                )}
              >
                {botList.map(bot => (
                  <MenuItem key={bot.id} value={bot.id}>
                    {bot.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            {formik.touched.botIds && formik.errors.botIds && (
              <Typography variant={"subtitle2"} color={"error"}>
                {formik.errors.botIds}
              </Typography>
            )}
          </Grid>
          <Grid item>
            <TextField
              label={"タイトル"}
              name={"title"}
              type={"text"}
              value={formik.values.title}
              onChange={formik.handleChange}
            />
            {formik.touched.title && formik.errors.title && (
              <Typography variant={"subtitle2"} color={"error"}>
                {formik.errors.title}
              </Typography>
            )}
          </Grid>
          <Grid item>
            <TextField
              label={"著者"}
              name={"author"}
              type={"text"}
              value={formik.values.author}
              onChange={formik.handleChange}
            />
            {formik.touched.author && formik.errors.author && (
              <Typography variant={"subtitle2"} color={"error"}>
                {formik.errors.author}
              </Typography>
            )}
          </Grid>
          <Grid item>
            <TextField
              label={"投稿日"}
              name={"postedAt"}
              type={"date"}
              value={formik.values.postedAt}
              InputLabelProps={{ shrink: true }}
              onChange={formik.handleChange}
            />
            {formik.touched.postedAt && formik.errors.postedAt && (
              <Typography variant={"subtitle2"} color={"error"}>
                {formik.errors.postedAt}
              </Typography>
            )}
          </Grid>
          <Grid item>
            <Button
              type={"submit"}
              disabled={formik.isSubmitting}
              color={"primary"}
            >
              登録
            </Button>
          </Grid>
        </Grid>
      </form>
    </Paper>
  );
};

export default RegisterPostForm;