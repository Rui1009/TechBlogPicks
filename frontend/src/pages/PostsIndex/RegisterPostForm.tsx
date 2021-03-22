import { Formik } from "formik";
import React, { useEffect, useState } from "react";
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
  TextField
} from "@material-ui/core";
import { format, getUnixTime, startOfToday } from "date-fns";
import { api } from "../../utils/Api";

type Bot = { id: string; name: string };

const mock = [
  {
    id: "1",
    name: "front"
  },
  {
    id: "2",
    name: "back"
  },
  {
    id: "3",
    name: "infra"
  }
];

const RegisterPostForm: React.FC = () => {
  const [botList, setBotList] = useState<Bot[]>([]);

  useEffect(() => {
    api.get<Bot[]>("http://localhost:9000/bots").then(v => {
      // setBotList(v.data.data);
      setBotList(mock);
      console.log(botList);
    });
  }, []);

  return (
    <Formik
      initialValues={{
        url: "",
        title: "",
        author: "",
        postedAt: format(startOfToday(), "yyyy-MM-dd"),
        botIds: []
      }}
      onSubmit={(values, { setSubmitting }) => {
        setTimeout(() => {
          const req = {
            ...values,
            postedAt: getUnixTime(new Date(values.postedAt))
          };
          alert(JSON.stringify(req, null, 2));
          setSubmitting(false);
        }, 400);
      }}
    >
      {({
        values,
        errors,
        touched,
        handleChange,
        handleBlur,
        handleSubmit,
        isSubmitting
        /* and other goodies */
      }) => (
        <Paper style={{ width: "60%" }}>
          <form onSubmit={handleSubmit}>
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
                  value={values.url}
                  onChange={handleChange}
                />
              </Grid>
              <Grid item style={{ width: "100%" }}>
                <FormControl fullWidth>
                  <InputLabel>カテゴリー</InputLabel>
                  <Select
                    multiple
                    value={values.botIds}
                    input={<Input />}
                    onChange={handleChange}
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
              </Grid>
              <Grid item>
                <TextField
                  label={"タイトル"}
                  name={"title"}
                  type={"text"}
                  value={values.title}
                  onChange={handleChange}
                />
              </Grid>
              <Grid item>
                <TextField
                  label={"著者"}
                  name={"author"}
                  type={"text"}
                  value={values.author}
                  onChange={handleChange}
                />
              </Grid>
              <Grid item>
                <TextField
                  label={"投稿日"}
                  name={"postedAt"}
                  type={"date"}
                  value={values.postedAt}
                  InputLabelProps={{ shrink: true }}
                  onChange={handleChange}
                />
              </Grid>
              <Grid item>
                <Button
                  type={"submit"}
                  disabled={isSubmitting}
                  color={"primary"}
                >
                  登録
                </Button>
              </Grid>
            </Grid>
          </form>
        </Paper>
      )}
    </Formik>
  );
};

export default RegisterPostForm;
