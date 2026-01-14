import { useEffect, useState } from "react";
import CircularButton from './CircularButton';
import Modal from './Modal';
import Settings from './Settings';
import Thumbnails from './Thumbnail';
import Years from './Years';
import MenuBar from "./MenuBar";

export default function App() {
  const [directoryData, setDirectoryData] = useState({year: 0, fileCount: 0});
  const [displaySettings, setDisplaySettings] = useState(false);
  const [settings, setSettings] = useState({pageSize: 4, sortType: 'DATE_ASC'});

    useEffect(() => {

      setDisplaySettings(false);


    }, [settings]
  );

  function onYearSelected(directoryData){
    setDirectoryData(directoryData);
  }

  function onSettingsClose(settings){
    setSettings(settings);
    setDisplaySettings(false);
  }

  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const toggleSidebar = () => {
    setIsSidebarOpen(!isSidebarOpen);
  };


  return (

    <div>
      <div style={{display: 'flex', alignItems: 'center', gap: 10}}>
        <h1>Family Photos</h1>
        <CircularButton color='red'>Years</CircularButton>
        <CircularButton color='blue'>Config</CircularButton>
        <CircularButton color='green'>Other</CircularButton>
      </div>

      <button onClick={toggleSidebar} className="toggle-btn">
        {isSidebarOpen ? 'Close Sidebar' : 'Open Sidebar'}
      </button>

      <MenuBar isOpen={isSidebarOpen} toggleSidebar={toggleSidebar} />
      
      <button onClick={()=>setDisplaySettings(true)}>Settings</button>
      {displaySettings &&
        <Modal display={true}>
          <Settings settings={settings} onClose={onSettingsClose}></Settings>      
        </Modal>
      }
      <Years onYearSelected={(dd)=>onYearSelected(dd)} />
      <Thumbnails year={directoryData.year} settings={settings} key={directoryData.year} />
    </div>
  );
}