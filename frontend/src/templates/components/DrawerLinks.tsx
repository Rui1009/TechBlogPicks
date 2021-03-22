import React from "react";
import { List, ListItem, ListItemIcon, ListItemText } from "@material-ui/core";
import { useHistory } from "react-router-dom";
import AssignmentIcon from "@material-ui/icons/Assignment";

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
    </List>
  );
};
