interface Props {
    enabled: boolean;
    onChange: () => void;
    disabled?: boolean;
}

export const ToggleSwitch = ({ enabled, onChange, disabled }: Props) => {
    return (
        <button
            type="button"
            role="switch"
            aria-checked={enabled}
            className={`switch ${enabled ? 'on' : 'off'}`}
            onClick={onChange}
            disabled={disabled}
        >
            <span className="switch-knob" />
        </button>
    );
};