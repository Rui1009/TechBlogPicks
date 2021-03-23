export type BotIndexResponse = {
  data: {
    id: string;
    name: string;
    clientId: string | null;
    clientSecret: string | null;
  }[];
};
