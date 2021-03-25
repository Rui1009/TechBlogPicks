const SERVER_URL = process.env.REACT_APP_SERVER_URL || "http://localhost:9000";

const root = (path?: string) => {
  return SERVER_URL + (path || "");
};

const BotsEndpoint = (path?: string) => {
  console.log(process.env.REACT_APP_SERVER_URL);
  const baseUrl = root("/bots");
  return baseUrl + (path || "");
};

const PostEndpoint = (path?: string) => {
  const baseUrl = root("/posts");
  return baseUrl + (path || "");
};

PostEndpoint.publish = (path?: string) => {
  const baseUrl = PostEndpoint("/publish");
  return baseUrl + (path || "");
};

export const Endpoints = {
  root,
  bots: BotsEndpoint,
  posts: PostEndpoint
};
