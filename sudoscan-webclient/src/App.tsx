import { WebCamPicture } from './pages/WebCamPicture'
import { NavMenu } from './components/NavMenu';

function App() {
  return (
    <div className="h-screen bg-gray-300 font-sans leading-normal tracking-normal">
      <NavMenu />
      <WebCamPicture/>
    </div>
  );
}

export default App;
