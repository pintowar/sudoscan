import { useRef, useState } from "react";
import { FaCamera, FaRegTrashAlt } from 'react-icons/fa';
import Webcam from "react-webcam";
import axios from 'axios';

import { AlertMessage } from "../components/AlertMessage";

export const WebCamPicture = () => {
    const noImage = "./no-image.png"
    const [imgSource, setImgSource] = useState(noImage);
    const [color, setColor] = useState('BLUE');
    const [processing, setProcessing] = useState(false);
    const [alert, setAlert] = useState(false);
    const webcamRef = useRef<Webcam>(null);
    const [imgWidth, imgHeight] = [640, 480]
    const imgStyle = {width: `${imgWidth}px`, height: `${imgHeight}px`}

    const capture = async () => {
        try {
            setProcessing(true)
            const screenshot = webcamRef.current?.getScreenshot() || ""
            const res = await axios.post('/api/solve', { encodedImage: screenshot, color });
            setImgSource(res.data)
            setProcessing(false)
        } catch (e) {
            console.log((e as Error).message)
            setAlert(true)
            setProcessing(false)
        }
    }

    const clean = () => {
        setImgSource(noImage)
    }

    return (
        <>
            { alert && <AlertMessage alert={alert} setAlert={setAlert} /> }

            <div className="flex justify-center mt-5 space-x-5" >
                <Webcam audio={false} ref={webcamRef} width={imgWidth} height={imgHeight} screenshotFormat="image/jpeg"/>
                <img src={imgSource} className="object-scale-down" style={imgStyle} alt="webcam-capture"/>
            </div>
            
            <div className="flex justify-center space-x-2">
                <select value={color} onChange={(e) => setColor(e.target.value)} className="px-4 py-3 my-5 bg-blue-500 hover:bg-blue-700 rounded-full text-white font-bold flex" >
                    <option value="BLUE" className="text-blue-500 bg-white">Blue</option>
                    <option value="GREEN" className="text-green-500 bg-white">Green</option>
                    <option value="RED" className="text-red-500 bg-white">Red</option>
                </select>

                <button onClick={capture} className="px-4 py-3 my-5 bg-blue-500 hover:bg-blue-700 rounded-full text-white font-bold flex">
                    <FaCamera className="h-6 w-6"/>
                    <span className="ml-2">Capture</span>
                </button>
                <button onClick={clean} className="px-4 py-3 my-5 bg-blue-500 hover:bg-blue-700 rounded-full text-white font-bold flex">
                    <FaRegTrashAlt className="h-6 w-6"/>
                    <span className="ml-2">Clean</span>
                </button>
                {processing && <div className="px-4 py-3 my-5 w-12 h-12 border-4 border-blue-500 rounded-full loader" />}
            </div>
            
        </>
    );
};