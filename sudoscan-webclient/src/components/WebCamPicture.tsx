import { useRef, useCallback, useState } from "react";
import { FaCamera, FaCalculator, FaRegTrashAlt } from 'react-icons/fa';
import Webcam from "react-webcam";

export const WebCamPicture = () => {
    const noImage = "./no-image.png"
    const [imgSource, setImgSource] = useState(noImage);
    const [alert, setAlert] = useState(false);
    const webcamRef = useRef<Webcam>(null);
    const [imgWidth, imgHeight] = [640, 480]
    const imgStyle = {width: `${imgWidth}px`, height: `${imgHeight}px`}

    const capture = useCallback(() => {
        const screenshot = webcamRef.current?.getScreenshot() || ""
        setImgSource(screenshot)
    }, [webcamRef])

    const clean = useCallback(() => {
        setImgSource(noImage)
    }, [noImage])

    return (
        <>
            { alert &&
                <div role="alert">
                    <div className="relative bg-red-500 text-white font-bold rounded-t px-4 py-2">
                        <strong>Danger</strong>
                        <span className="absolute top-0 right-0 px-4 py-2" onClick={() => setAlert(!alert)}>
                            <button>X</button>
                        </span>
                    </div>
                    <div className="border border-t-0 border-red-400 rounded-b bg-red-100 px-4 py-3 text-red-700">
                        <p>Something not ideal might be happening.</p>
                    </div>
                </div>    
            }

            <div className="flex justify-center mt-5 space-x-5" >
                <Webcam audio={false} ref={webcamRef} width={imgWidth} height={imgHeight} screenshotFormat="image/jpeg"/>
                <img src={imgSource} className="object-scale-down" style={imgStyle} alt="webcam-capture"/>
            </div>
            
            <div className="flex justify-center space-x-2">
                <button onClick={capture} className="px-4 py-3 my-5 bg-blue-500 hover:bg-blue-700 rounded-full text-white font-bold flex">
                    <FaCamera className="h-6 w-6"/>
                    <span className="ml-2">Capture</span>
                </button>
                <button className="px-4 py-3 my-5 bg-blue-500 hover:bg-blue-700 rounded-full text-white font-bold flex">
                    <FaCalculator className="h-6 w-6"/>
                    <span className="ml-2">Solve</span>
                </button>
                <button onClick={clean} className="px-4 py-3 my-5 bg-blue-500 hover:bg-blue-700 rounded-full text-white font-bold flex">
                    <FaRegTrashAlt className="h-6 w-6"/>
                    <span className="ml-2">Clean</span>
                </button>
            </div>
            
        </>
    );
};