import React from 'react';

function JsonText() {
  const payload = {
    nome: "Romy",
    corso: "Business Intelligence",
    attivo: true,
    eta: 27
  };

  const formatValue = (value) => {
    if (typeof value === 'string') return `"${value}"`;
    if (value === null) return 'null';
    return String(value);
  };

  return (
    <pre className="json-box">
      {'{\n'}
      {Object.entries(payload).map(([key, value], index, array) => (
        <React.Fragment key={key}>
          {'  '}
          <span className="json-key">"{key}"</span>
          <span className="json-punctuation">: </span>
          <span className="json-value">{formatValue(value)}</span>
          {index < array.length - 1 ? ',' : ''}
          {'\n'}
        </React.Fragment>
      ))}
      {'}'}
    </pre>
  );
}


export default JsonText