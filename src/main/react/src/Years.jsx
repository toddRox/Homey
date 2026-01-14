import React, { useEffect, useState } from "react";
import axios from "axios";

export default function Years({onYearSelected}) {
  const [directoryData, setDirectoryData] = useState([]);

  useEffect(() => {

    axios
        .get("/directory-data")
        .then((response) => {
          setDirectoryData(response.data);
        })
        .catch((err) => {
          console.error(err);
        });
    }, []
  );

  function handleClick(directoryData) {
    onYearSelected(directoryData);
  }

  return (
    <div>
      {directoryData.filter(dd=>dd.fileCount > 0).map((dd) => (
        <button onClick={()=>handleClick(dd)} key={dd.year} year={dd.year}>{dd.year}</button>
      ))}
    </div>
  );
}
