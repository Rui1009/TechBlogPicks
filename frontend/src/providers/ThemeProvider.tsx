import React from "react";
import {
  createMuiTheme,
  CssBaseline,
  MuiThemeProvider
} from "@material-ui/core";

const theme = createMuiTheme({
  palette: {
    primary: {
      main: "#d32f2f",
      contrastText: "#ffffff",
      light: "#d32f2f",
      dark: "#d32f2f"
    }
  }
});

export const ThemeProvider: React.FC<unknown> = ({ children }) => {
  return (
    <MuiThemeProvider theme={theme}>
      <CssBaseline />
      {children}
    </MuiThemeProvider>
  );
};
