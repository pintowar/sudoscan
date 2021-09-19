import { WebCamPicture } from './pages/WebCamPicture'
import { WebCamStream } from './pages/WebCamStream'
import { NavMenu } from './components/NavMenu'
import {
  HashRouter as Router,
  Switch,
  Route
} from "react-router-dom"

function App() {
  return (
    <Router>
      <div className="h-screen bg-gray-300 font-sans leading-normal tracking-normal">
        <NavMenu />
        <Switch>
            <Route exact path="/">
              <WebCamStream/>
            </Route>
            <Route path="/picture">
              <WebCamPicture/>
            </Route>
            <Route path="/stream">
              <WebCamStream/>
            </Route>
          </Switch>  
      </div>  
    </Router>
  );
}

export default App;
