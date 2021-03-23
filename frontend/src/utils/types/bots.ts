export type BotIndexResponse = {
  data: {
    id: string;
    name: string;
    clientId?: string;
    clientSecret?: string;
  }[];
};
