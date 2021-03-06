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
  },
  drawerClose: {
    justifyContent: "flex-start"
  },
  menu: {
    color: "#fff"
  }
});

export const AppTemplate: React.FC = () => {
  const classes = useStyles();

  const [drawerOpen, setDrawerOpen] = useState(false);
  return (
    <>
      <AppBar position={"static"}>
        <Toolbar>
          <IconButton edge={"start"} onClick={() => setDrawerOpen(true)}>
            <MenuIcon className={classes.menu} />
          </IconButton>
          <Typography variant={"h5"}>Winkie</Typography>
        </Toolbar>
      </AppBar>
      <Drawer open={drawerOpen} classes={{ paper: classes.drawer }}>
        <IconButton
          onClick={() => setDrawerOpen(false)}
          className={classes.drawerClose}
        >
          <ChevronLeftIcon />
        </IconButton>
        <Divider />
        <DrawerLinks />
      </Drawer>
    </>
  );
};
