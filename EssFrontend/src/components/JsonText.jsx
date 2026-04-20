JsonText.propTypes = {
    data: PropTypes.oneOfType([
        PropTypes.object,
        PropTypes.array,
    ]).isRequired,
};

function JsonText({ data }) {
  return (
    <pre className="json-box">
      {JSON.stringify(data, null, 2)}
    </pre>
  );
}

export default JsonText;