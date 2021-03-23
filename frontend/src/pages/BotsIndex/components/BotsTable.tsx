import React, { Dispatch, SetStateAction } from "react";
import MaterialTable from "material-table";
import { BotIndexResponse } from "../../../utils/types/bots";
import { useFormikContext } from "formik";

type Props = {
  botList: BotIndexResponse["data"];
  setSelectedBot: Dispatch<
    SetStateAction<BotIndexResponse["data"][number] | undefined>
  >;
  setModalOpen: Dispatch<SetStateAction<boolean>>;
};

export const BotsTable: React.FC<Props> = ({
  botList,
  setSelectedBot,
  setModalOpen
}) => {
  return (
    <MaterialTable
      style={{ width: "90%", margin: "auto", padding: "0 16px" }}
      title={"Bot一覧"}
      columns={[
        {
          title: "Bot名",
          field: "name"
        },
        {
          title: "clientID",
          field: "clientId"
        },
        {
          title: "clientSecret",
          field: "clientSecret"
        }
      ]}
      data={botList}
      actions={[
        {
          icon: "edit",
          onClick: (evt, data) => {
            setSelectedBot(data instanceof Array ? data[0] : data);
            setModalOpen(true);
          }
        },
        {
          icon: "add",
          isFreeAction: true,
          onClick: () => {
            setModalOpen(true);
          }
        }
      ]}
    />
  );
};
