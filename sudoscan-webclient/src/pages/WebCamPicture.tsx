import { useEffect, useRef, useState } from "react";
import { FaCamera, FaRegTrashAlt } from 'react-icons/fa';
import Webcam from "react-webcam";
import axios from 'axios';

import { AlertMessage } from "../components/AlertMessage";

class EngineInfo {
    solver: string = "Unknown"
    recognizer: string = "Unknown"
}

export const WebCamPicture = () => {
    const noImage = "./no-image.png"
    const [imgSource, setImgSource] = useState(noImage);
    const [color, setColor] = useState('BLUE');
    const [processing, setProcessing] = useState(false);
    const [info, setInfo] = useState(new EngineInfo());
    const [alert, setAlert] = useState("");
    const webcamRef = useRef<Webcam>(null);
    const [imgWidth, imgHeight] = [480, 360]
    const videoConstraints = {
        width: imgWidth,
        height: imgHeight,
        facingMode: "environment"
      };
    const imgStyle = {width: `${imgWidth}px`, height: `${imgHeight}px`}

    useEffect(() => {
        const showInfo = async () => {
            const res = await axios.get<EngineInfo>('/api/engine-info')
            setInfo(res.data)
        }

        showInfo()
      }, []);

    const capture = async () => {
        try {
            setProcessing(true)
            const screenshot = webcamRef.current?.getScreenshot() || ""
            const res = await axios.post('/api/solve', { encodedImage: screenshot, color })
            setImgSource(res.data)
        } catch ({ response: {data, status} }) {
            setAlert(status === 500 ? data as string : "Something wrong happened!!")            
        } finally {
            setProcessing(false)
        }
    }

    const clean = () => {
        setImgSource(noImage)
    }

    return (
        <div className="container pt-16 mx-auto items-center">
            { alert && <AlertMessage message={alert} setMessage={setAlert} /> }

            <div className="flex flex-wrap justify-center space-x-5 pt-4">
                Engine info: {info.recognizer} / {info.solver}
            </div>

            <div className="flex flex-wrap justify-center space-x-5 pt-4">
                <Webcam audio={false} ref={webcamRef} videoConstraints={videoConstraints} screenshotFormat="image/jpeg"/>
                <img src={imgSource} className="object-scale-down" style={imgStyle} alt="webcam-capture"/>
            </div>
            
            <div className="flex flex-wrap justify-center py-5 space-x-5">
                <select value={color} onChange={(e) => setColor(e.target.value)} className="px-4 py-3 my-5 bg-gray-600 hover:bg-gray-800 rounded-full text-white font-bold flex" >
                    <option value="BLUE" className="text-blue-500 bg-white">Blue</option>
                    <option value="GREEN" className="text-green-500 bg-white">Green</option>
                    <option value="RED" className="text-red-500 bg-white">Red</option>
                </select>

                <button disabled={processing} onClick={capture} className="px-4 py-3 my-5 bg-gray-600 hover:bg-gray-800 rounded-full text-white font-bold flex">
                    <FaCamera className="h-6 w-6"/>
                    <span className="ml-2">Capture</span>
                </button>

                <button disabled={processing} onClick={clean} className="px-4 py-3 my-5 bg-gray-600 hover:bg-gray-800 rounded-full text-white font-bold flex">
                    <FaRegTrashAlt className="h-6 w-6"/>
                    <span className="ml-2">Clean</span>
                </button>
                {processing && <div className="px-4 py-3 my-5 w-12 h-12 border-4 border-gray-600 rounded-full loader" />}
            </div>
        </div>
    );
};