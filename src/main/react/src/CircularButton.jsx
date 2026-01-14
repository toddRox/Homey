import React from 'react';

export default function CircularButton({ onClick, color = 'blue', size = 60, children }) {

  return (
    <button
      onClick={onClick}
      style={{
        backgroundColor: color,
        width: `${size}px`,
        height: `${size}px`,
        borderRadius: '50%',
        border: 'none',
        color: '#fff',
        fontSize: '16px',
        cursor: 'pointer',
        display: 'inline',
        alignItems: 'center',
        justifyContent: 'center',
        outline: 'black',
      }}
    >
      {children}
    </button>
  );
}
