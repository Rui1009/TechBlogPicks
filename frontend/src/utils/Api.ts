import axios, { AxiosInstance } from "axios";
import { ApiResponse } from "./types";

class Api {
  constructor() {
    this.axios = axios.create();
  }

  private axios: AxiosInstance;

  get<Data>(...args: Parameters<typeof axios.get>) {
    return this.axios.get<unknown, ApiResponse<Data>>(...args);
  }

  post<Data>(...args: Parameters<typeof axios.post>) {
    return this.axios.post<unknown, ApiResponse<Data>>(...args);
  }
}

const api = new Api();

export { api };
