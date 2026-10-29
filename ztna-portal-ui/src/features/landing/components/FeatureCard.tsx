import type { Feature } from '../types';

interface FeatureCardProps {
    feature: Feature;
}

const FeatureCard = ({ feature }: FeatureCardProps) => {
    return (
        <div className="ov-feature-card">
            <div className="ov-feature-icon">{feature.icon}</div>
            <h3 className="ov-feature-title">{feature.title}</h3>
            <p className="ov-feature-desc">{feature.description}</p>
        </div>
    );
}

export default FeatureCard;