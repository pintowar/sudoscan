import { useRef, useState, useEffect, useCallback } from "react";
import useInterval from "../utils/useInterval";
import Webcam from "react-webcam";
import { EngineInfoLabel } from "../components/EngineInfoLabel";

export const WebCamStream = () => {
    const host = process.env.NODE_ENV !== "production" ? process.env.REACT_APP_WS_PROXY : window.location.host
    const wsUrl = `ws://${host}/ws/sudoku`
    const [socket, setSocket] = useState(new WebSocket(wsUrl))
    const [isConnected, setConnected] = useState(false)

    const setupSocket = useCallback(() => {
        if(socket.readyState === WebSocket.CLOSED) {
            setSocket(new WebSocket(wsUrl))
        }

        socket.addEventListener("open", (e) => {
            setConnected(true)
        })

        socket.addEventListener("message", (e) => {
            setImgSource(e.data)
        })

        socket.addEventListener("error", (e) => {
            console.error(e)
            setConnected(false)
            socket.close()
            clean()
        })

        socket.addEventListener("close", (e) => {
            setConnected(false)
            clean()
        })
    }, [socket, wsUrl])

    useEffect(() => {  
        setupSocket()

        return () => {
            socket.close()
        }
    }, [socket, setupSocket]);
    

    const noImage = "./no-image.png"
    const [imgSource, setImgSource] = useState(noImage);
    const [color, setColor] = useState('BLUE');
    const webcamRef = useRef<Webcam>(null);
    const [imgWidth, imgHeight] = [480, 360]
    const videoConstraints = {
        width: imgWidth,
        height: imgHeight,
        facingMode: "environment"
      };
    const imgStyle = {width: `${imgWidth}px`, height: `${imgHeight}px`}

    const clean = () => {
        setImgSource(noImage)
    }

    useInterval(() => { 
        if(WebSocket.OPEN === socket.readyState && webcamRef.current) {
            const screenshot = webcamRef.current.getScreenshot()
            const msg = JSON.stringify({ encodedImage: screenshot, color })
            console.debug("Message size: " + msg.length)
            socket.send(msg)
        } else if(WebSocket.CLOSED === socket.readyState) {
            console.debug("Reconnecting...")
            setupSocket()
        }
    }, 500)

    return (
        <div className="container pt-16 mx-auto items-center">

            <EngineInfoLabel/>

            <div className="flex flex-wrap justify-center py-5 space-x-5">
                {isConnected === true ? <div className="px-4 py-3 my-5 w-10 h-10 bg-green-600 rounded-full" /> : <div className="px-4 py-3 my-5 w-10 h-10 bg-red-600 rounded-full" />}

                <select value={color} onChange={(e) => setColor(e.target.value)} className="px-4 py-3 my-5 bg-gray-600 hover:bg-gray-800 rounded-full text-white font-bold flex" >
                    <option value="BLUE" className="text-blue-500 bg-white">Blue</option>
                    <option value="GREEN" className="text-green-500 bg-white">Green</option>
                    <option value="RED" className="text-red-500 bg-white">Red</option>
                </select>
            </div>

            <div className="flex flex-wrap justify-center space-x-5 pt-4">
                <Webcam audio={false} ref={webcamRef} videoConstraints={videoConstraints} />
                <img src={imgSource} className="object-scale-down" style={imgStyle} alt="webcam-capture"/>
            </div>
        </div>
    );
};