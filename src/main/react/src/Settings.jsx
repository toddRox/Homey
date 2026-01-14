import React, { useEffect, useState } from "react";
import axios from "axios";

export default function Settings({settings, onClose}) {
  const pageSizes = [4, 25, 50, 75, 100, 150]
  const sortTypes = ['DATE_ASC', 'DATE_DEC']
  const [tempSettings, setTempSettings] = useState({...settings});


  function save(){
    onClose(tempSettings);
  }

  function set(key, value){
    setTempSettings({...tempSettings, [key]: value})
  }

  return (
    <form style={{}}>
      <label htmlFor="ps" style={{}}>
        Page Size:
      </label>

      <select id="ps" value={tempSettings.pageSize} onChange={(e) => set('pageSize', Number(e.target.value))}>
        {pageSizes.map((size) => (<option value={size} key={size}>{size}</option>))}
      </select>

      <label htmlFor="st" style={{}}>
        Sort Type:
      </label>

      <select id="st" value={tempSettings.sortType} onChange={(e) => set('sortType', e.target.value)}>
        {sortTypes.map((sort) => (<option value={sort} key={sort}>{sort}</option>))}
      </select>


      <button type="submit" onClick={save}>Save</button>
    </form>
  );
}

 
