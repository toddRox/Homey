import React from 'react';

export default function({ display, onClose, children }){
  if (!display) return null;

  return (
      <div onClick={onClose} style={{
            position: 'fixed',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            background: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
        }}
      >
        <div style={{
              background: 'white',
              maxHeight: '95vh',
              maxWidth: '95vw',
              margin: 'auto',
              padding: '2%',
              border: '2px solid #000',
              borderRadius: '5px',
              boxShadow: '2px solid black',
          }}
        >
          {children}
          
        </div>
      </div>
    );
};
