function JsonText({ data }) {
  return (
    <pre className="json-box">
      {JSON.stringify(data, null, 2)}
    </pre>
  );
}

export default JsonText;