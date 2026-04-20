import PropTypes from 'prop-types';

Button.propTypes = {
    text: PropTypes.string.isRequired,
    onClick: PropTypes.func,
    variant: PropTypes.string,
    type: PropTypes.string,
    disabled: PropTypes.bool,
};

Button.defaultProps = {
    onClick: undefined,
};

function Button({text, onClick, variant = 'primary', type="button", disabled = false}) {

    return (

    <button className={`button button--${variant}`} onClick={onClick} type={type}>
      {text}
    </button>

    )
}

export default Button