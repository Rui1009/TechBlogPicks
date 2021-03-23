import React, { useEffect, useState } from "react";
import { UpdateBotFormModal } from "./components/UpdateBotFormModal";
import { BotsTable } from "./components/BotsTable";
import { api } from "../../utils/Api";
import { BotIndexResponse } from "../../utils/types/bots";

export const BotsIndex: React.FC = () => {
  const [botList, setBotList] = useState<BotIndexResponse["data"]>([]);
  const [selectedBot, setSelectedBot] = useState<
    BotIndexResponse["data"][number] | undefined
  >(undefined);

  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => {
    api.get<BotIndexResponse>("http://localhost:9000/bots").then(v => {
      setBotList(v.data.data);
    });
  }, []);

  return (
    <div style={{ padding: 32 }}>
      <UpdateBotFormModal
        {...{ selectedBot, modalOpen, setModalOpen, setSelectedBot }}
      />
      <BotsTable {...{ botList, setSelectedBot, setModalOpen }} />
    </div>
  );
};
