import React, { Dispatch, SetStateAction, useEffect, useState } from "react";
import { PostsIndexResponse } from "../../../utils/types/posts";
import useAutoCloseSnack from "../../../hooks/useAutoCloseSnack";
import { api } from "../../../utils/Api";
import MaterialTable from "material-table";
import { Typography } from "@material-ui/core";

type Props = {
  fetchedPosts: PostsIndexResponse["data"];
  setPosts: Dispatch<SetStateAction<PostsIndexResponse["data"]>>;
};

export const PostsTable: React.FC<Props> = ({ setPosts, fetchedPosts }) => {
  const { successSnack, errorSnack } = useAutoCloseSnack();

  const deletePosts = (postIds: number[]) =>
    api
      .delete("http://localhost:9000/posts", { data: { ids: postIds } })
      .then(() => successSnack("削除が完了しました"))
      .then(fetchPosts)
      .catch(e => {
        errorSnack(e.message);
      });

  const fetchPosts = () =>
    api
      .get<PostsIndexResponse>("http://localhost:9000/posts")
      .then(r => setPosts(r.data.data));

  useEffect(() => {
    fetchPosts();
  }, []);

  return (
    <MaterialTable
      style={{ width: "90%", margin: "auto", padding: "0 16px" }}
      columns={[
        {
          title: "タイトル",
          field: "title"
        },
        {
          title: "URL",
          field: "url",
          render: rowData => (
            <a href={rowData.url} target={"_blank"} rel="noreferrer">
              {rowData.url}
            </a>
          )
        },
        { title: "著者", field: "author" },
        {
          title: "投稿日時",
          field: "postedAt",
          type: "numeric",
          render: rowData => (
            <Typography>
              {new Date(rowData.postedAt * 1000).toLocaleDateString()}
            </Typography>
          )
        },
        {
          title: "登録日時",
          field: "createdAt",
          render: rowData => (
            <Typography>
              {new Date(rowData.createdAt * 1000).toLocaleDateString()}
            </Typography>
          )
        }
      ]}
      data={fetchedPosts}
      title={"記事一覧"}
      options={{
        selection: true
      }}
      actions={[
        {
          icon: "delete",
          onClick: (evt, data) => {
            deletePosts(
              data instanceof Array ? data.map(d => d.id) : [data.id]
            );
          }
        }
      ]}
      localization={{
        toolbar: {
          nRowsSelected: "{0}件を選択中"
        }
      }}
    />
  );
};
