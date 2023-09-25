import React from 'react';
import colors from 'src/styles/colors';

type JsonViewerProps = {
  data: object | null;
};

const JsonViewer: React.FC<JsonViewerProps> = ({ data }) => {
  const renderJson = (data: object | null, indent: number = 0) => {
    if (typeof data === 'object' && data !== null) {
      return (
        <div style={{ paddingLeft: `${indent * 20}px` }}>
          {Array.isArray(data)
            ? data.map((item, index) => (
                <div key={index}>{renderJson(item, indent + 1)}</div>
              ))
            : Object.entries(data).map(([key, value]) => (
                <div key={key}>
                  <span style={{ color: colors.primary[400] }}>{key}:</span>{' '}
                  {typeof value === 'object' ? (
                    renderJson(value, indent + 1)
                  ) : (
                    <span style={{ color: colors.primary[900] }}>
                      {JSON.stringify(value)}
                    </span>
                  )}
                </div>
              ))}
        </div>
      );
    } else {
      return <span>{JSON.stringify(data)}</span>;
    }
  };

  return <pre>{renderJson(data)}</pre>;
};

export default JsonViewer;
