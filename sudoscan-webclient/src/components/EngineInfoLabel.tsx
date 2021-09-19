import { useEffect, useState } from "react";
import axios from 'axios';

class EngineInfo {
    solver: string = "Unknown"
    recognizer: string = "Unknown"
}

export const EngineInfoLabel = () => {

    const [info, setInfo] = useState(new EngineInfo());

    useEffect(() => {
        const showInfo = async () => {
            const res = await axios.get<EngineInfo>('/api/engine-info')
            setInfo(res.data)
        }

        showInfo()
      }, []);

    return (
        <div className="flex flex-wrap justify-center space-x-5 pt-4">
            Engine info: {info.recognizer} / {info.solver}
        </div>
    );
};