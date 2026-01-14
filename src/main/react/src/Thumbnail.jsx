import React, { useEffect, useState } from "react";
import axios from "axios";
import Modal from "./Modal";

export default function Thumbnails({year, settings}) {
  const [imageNames, setImageNames] = useState([]);
  const [imageSrc, setImageSrc] = useState(null);
  const [open, setOpen] = useState(false);
  const [page, setPage] = useState(0);


  useEffect(() => {
    
    if(year > 0) {
      axios
        .get(`/image-names/${year}?pg=${page}&ps=${settings.pageSize}&st=${settings.sortType}`)
        .then((response) => {
          setImageNames(response.data);
        })
        .catch((err) => {
          console.error(err);
        });
    }}, [year, page]
  );

  function onClick(imageSrc){
    setImageSrc(imageSrc);
    setOpen(true);
  }

  function movePage(amount){
    setPage(page + amount);
  }

  return (
    <div>
      <Modal display={open} onClose={()=>setOpen(false)}>
        <img src={`${imageSrc}`} style={{display: 'block', margin: 'auto', maxHeight: '90vh', maxWidth: '90vw'}} />
      </Modal>

      {imageNames.map((name, index) => (
        <button onClick={()=>onClick(`/image/${year}/${name}`)} key={index}>
          <img src={`/image/${year}/${name}?tn=1`} />
        </button>
      ))}

      <button onClick={()=>movePage(1)}>&gt;&gt;</button>
    </div>
  );
}
