import React, { useEffect, useState } from "react";
import MaterialTable from "material-table";
import { api } from "../../utils/Api";
import { PostsIndexResponse } from "../../utils/types/posts";
import { Grid } from "@material-ui/core";
import RegisterPostForm from "./RegisterPostForm";

const mock = [
  {
    id: 1,
    title: "test title",
    url: "https:yahoo.com",
    author: "test author",
    postedAt: 10000,
    createdAt: 1222
  },
  {
    id: 2,
    title: "sample title",
    url: "https:google.com",
    author: "sampler",
    postedAt: 100,
    createdAt: 122333
  }
];

export const PostsIndex: React.FC = () => {
  const [fetchedPosts, setFetchedPosts] = useState<PostsIndexResponse["data"]>(
    []
  );

  useEffect(() => {
    api.get<PostsIndexResponse>("http://localhost:9000/posts").then(
      r => setFetchedPosts(r.data.data)
      // setFetchedPosts(mock)
    );
  }, []);

  return (
    <Grid
      container
      direction={"column"}
      justify={"center"}
      alignItems={"center"}
      style={{ padding: 24 }}
    >
      <Grid item style={{ width: "100%", margin: "16px 0" }}>
        <RegisterPostForm setPosts={setFetchedPosts} />
      </Grid>
      <Grid item style={{ width: "100%" }}>
        <MaterialTable
          style={{ width: "90%", margin: "auto", padding: "0 16px" }}
          columns={[
            {
              title: "タイトル",
              field: "title"
            },
            { title: "URL", field: "url" },
            { title: "著者", field: "author" },
            { title: "投稿日時", field: "postedAt", type: "numeric" },
            { title: "登録日時", field: "createdAt", type: "numeric" }
          ]}
          data={fetchedPosts}
          title={"記事一覧"}
        />
      </Grid>
    </Grid>
  );
};
