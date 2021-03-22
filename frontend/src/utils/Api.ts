import axios, { AxiosInstance, AxiosResponse } from "axios";

class Api {
  constructor() {
    this.axios = axios.create();
  }

  private axios: AxiosInstance;

  get<Data>(...args: Parameters<typeof axios.get>) {
    return this.axios.get<unknown, AxiosResponse<Data>>(...args);
  }

  post<Data>(...args: Parameters<typeof axios.post>) {
    return this.axios.post<unknown, AxiosResponse<Data>>(...args);
  }
}

const api = new Api();

export { api };
