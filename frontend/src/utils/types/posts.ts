export type PostsIndexResponse = {
  data: {
    id: number;
    url: string;
    title: string;
    author: string;
    postedAt: number;
    createdAt: number;
  }[];
};
