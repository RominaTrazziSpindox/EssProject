function Button({text, handleClick, variant = 'primary'}) {

    return (

    <button className={`button button--${variant}`} onClick={handleClick} type="button">
      {text}
    </button>

    )
}

export default Button