interface Props {
    checked: boolean;
    onChange: () => void;
}

export const ToggleSwitch = ({ checked, onChange }: Props) => {
    return (
        <label className="toggle-switch" onClick={(e) => e.stopPropagation()}>
            <input type="checkbox" checked={checked} onChange={onChange} />
            <span className="slider round"></span>
        </label>
    );
};