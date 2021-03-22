import React from "react";
import { BrowserRouter, Route, Switch } from "react-router-dom";
import "./App.css";
import { PostsIndex } from "./pages/PostsIndex/PostsIndex";
import { AppTemplate } from "./templates/Apptemplate";

function App() {
  return (
    <BrowserRouter>
      <AppTemplate />
      <Switch>
        <Route exact path="/" component={PostsIndex} />
      </Switch>
    </BrowserRouter>
  );
}

export default App;
