import { LANDING_DATA } from '../constants/landingData';
import FeatureCard from './FeatureCard';

const FeaturesSection = () => {
    return (
        <div className="ov-features">
            <div className="ov-features-grid">
                {LANDING_DATA.map((feature) => (
                    <FeatureCard key={feature.id} feature={feature} />
                ))}
            </div>
        </div>
    );
}

export default FeaturesSection;
