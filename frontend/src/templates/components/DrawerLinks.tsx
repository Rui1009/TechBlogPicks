import React from "react";
import { List, ListItem, ListItemIcon, ListItemText } from "@material-ui/core";
import { useHistory } from "react-router-dom";
import AssignmentIcon from "@material-ui/icons/Assignment";
import RedditIcon from "@material-ui/icons/Reddit";

export const DrawerLinks: React.FC = () => {
  const history = useHistory();
  return (
    <List>
      <ListItem button onClick={() => history.push("/")}>
        <ListItemIcon>
          <AssignmentIcon />
        </ListItemIcon>
        <ListItemText primary={"記事を登録する"} />
      </ListItem>
      <ListItem button onClick={() => history.push("/bots")}>
        <ListItemIcon>
          <RedditIcon />
        </ListItemIcon>
        <ListItemText primary={"Botを管理する"} />
      </ListItem>
    </List>
  );
};
