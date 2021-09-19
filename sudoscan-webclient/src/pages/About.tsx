
export const About = () => {
    const [imgWidth, imgHeight] = [140, 60]
    const imgStyle = {width: `${imgWidth}px`, height: `${imgHeight}px`}

    return (
        
        <div className="container pt-16 mx-auto items-center">
            <div className="flex flex-col items-center justify-center">
                <h1 className="text-6xl leading-normal">Sudoscan Web</h1>
                <p className="leading-normal mb-12">
                        This Project contains a series of components. Here ia a list of the main components used by the project.
                </p>

                <h2 className="text-3xl leading-normal">Solvers</h2>
                <ul>
                    <li>
                        <a href="https://choco-solver.org/" target="_blank" rel="noreferrer">
                            <img src="https://choco-solver.org/images/logo-choco-small.webp" 
                                alt="Choco Solver" title="Choco Solver" style={imgStyle} className="object-scale-down"/>
                        </a>
                    </li>
                </ul>
        
                <h2 className="text-3xl leading-normal">Recognizers</h2>
                <ul>
                    <li>
                        <a href="https://deeplearning4j.org/" target="_blank" rel="noreferrer">
                            <img src="https://deeplearning4j.org/images/logo.png" 
                                alt="DeepLearning4j" title="DeepLearning4j" style={imgStyle} className="object-scale-down"/>
                        </a>
                    </li>
                    <li>
                        <a href="https://djl.ai/" target="_blank" rel="noreferrer">
                            <img src="https://djl.ai/website/img/djl-middle.png" 
                                alt="Deep Java Library" title="Deep Java Library" style={imgStyle} className="object-scale-down"/>
                        </a>
                    </li>
                </ul>

                <h2 className="text-3xl leading-normal">Server Side</h2>
                <ul>
                    <li>
                        <a href="https://micronaut.io/" target="_blank" rel="noreferrer">
                            <img src="https://objectcomputing.com/files/2716/2256/3799/micronaut_stacked_black.png" 
                                alt="Micronaut" title="Micronaut" style={imgStyle} className="object-scale-down"/>
                        </a>
                    </li>
                </ul>

                <h2 className="text-3xl leading-normal">Client Side</h2>
                <ul>
                    <li>
                        <a href="https://reactjs.org/" target="_blank" rel="noreferrer">
                            <img src="https://www.vectorlogo.zone/logos/reactjs/reactjs-icon.svg" 
                                alt="React" title="React" style={imgStyle} className="object-scale-down"/>
                        </a>
                    </li>
                </ul>

            </div>
        </div>
        
    );
};