import { PostsTable } from "./components/PostsTable";
import React, { useEffect, useState } from "react";
import { api } from "../../utils/Api";
import { PostsIndexResponse } from "../../utils/types/posts";
import { Grid } from "@material-ui/core";
import RegisterPostForm from "./RegisterPostForm";

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
        <PostsTable />
      </Grid>
    </Grid>
  );
};
