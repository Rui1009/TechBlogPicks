import { PostsTable } from "./components/PostsTable";
import React, { useState } from "react";
import { PostsIndexResponse } from "../../utils/types/posts";
import { Grid } from "@material-ui/core";
import RegisterPostForm from "./RegisterPostForm";
import { Endpoints } from "../../constants/Endpoints";

export const PostsIndex: React.FC = () => {
  const [fetchedPosts, setFetchedPosts] = useState<PostsIndexResponse["data"]>(
    []
  );

  return (
    <Grid
      container
      direction={"column"}
      justify={"center"}
      alignItems={"center"}
      style={{ padding: 32 }}
    >
      <Grid item style={{ width: "100%", paddingBottom: 16 }}>
        <RegisterPostForm setPosts={setFetchedPosts} />
      </Grid>
      <Grid item style={{ width: "100%" }}>
        <PostsTable fetchedPosts={fetchedPosts} setPosts={setFetchedPosts} />
      </Grid>
      <button onClick={() => fetch(Endpoints.posts.publish())}>aaa</button>
    </Grid>
  );
};
