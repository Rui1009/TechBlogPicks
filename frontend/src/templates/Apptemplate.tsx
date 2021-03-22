import React, { useState } from "react";
import {
  AppBar,
  Toolbar,
  IconButton,
  Drawer,
  Divider,
  Typography
} from "@material-ui/core";
import MenuIcon from "@material-ui/icons/Menu";
import ChevronLeftIcon from "@material-ui/icons/ChevronLeft";
import { makeStyles } from "@material-ui/core/styles";
import { DrawerLinks } from "./components/DrawerLinks";

const useStyles = makeStyles({
  drawer: {
    width: 240
  }
});

export const AppTemplate: React.FC<unknown> = props => {
  const classes = useStyles();

  const [drawerOpen, setDrawerOpen] = useState(false);
  return (
    <>
      <AppBar position={"static"}>
        <Toolbar>
          <IconButton edge={"start"} onClick={() => setDrawerOpen(true)}>
            <MenuIcon />
          </IconButton>
          <Typography variant={"h5"}>Winkie</Typography>
        </Toolbar>
      </AppBar>
      <Drawer open={drawerOpen} classes={{ paper: classes.drawer }}>
        <div onClick={() => setDrawerOpen(false)}>
          <IconButton onClick={() => setDrawerOpen(false)}>
            <ChevronLeftIcon />
          </IconButton>
        </div>
        <Divider />
        <DrawerLinks />
      </Drawer>
    </>
  );
};
