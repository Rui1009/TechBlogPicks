import { AxiosResponse } from "axios";

export type ApiResponse<Data> = AxiosResponse<{ data: Data }>;
