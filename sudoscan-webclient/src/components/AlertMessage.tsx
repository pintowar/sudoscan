interface AlertMessageProps {
    message: string;
    setMessage: (val: string) => void;
}

export const AlertMessage = (props: AlertMessageProps) => {

    return (
        <div role="alert" className="pb-2">
            <div className="relative bg-red-500 text-white font-bold rounded-t px-4 py-2 ">
                <strong>Danger</strong>
                <span className="absolute top-0 right-0 px-4 py-2" onClick={() => props.setMessage("")}>
                    <button>X</button>
                </span>
            </div>
            <div className="border border-t-0 border-red-400 rounded-b bg-red-100 px-4 py-3 text-red-700">
                <p>{props.message}</p>
            </div>
        </div>   
    )

};
