import React from "react";
import { BrowserRouter, Route, Switch } from "react-router-dom";
import "./App.css";
import { PostsIndex } from "./pages/PostsIndex/PostsIndex";
import { AppTemplate } from "./templates/Apptemplate";
import { BotsIndex } from "./pages/BotsIndex/BotsIndex";

function App() {
  return (
    <BrowserRouter>
      <AppTemplate />
      <Switch>
        <Route exact path="/" component={PostsIndex} />
        <Route path="/bots" component={BotsIndex} />
      </Switch>
    </BrowserRouter>
  );
}

export default App;
