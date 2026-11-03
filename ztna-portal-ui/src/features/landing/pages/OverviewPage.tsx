import FeaturesSection from '../components/FeaturesSection';
import HeroSection from '../components/HeroSection';
import '../styles/OverviewPage.css'; // <-- Dòng này cực kỳ quan trọng

const OverviewPage = () => {
    return (
        <div className="ov-page">
            <HeroSection />
            <FeaturesSection />
        </div>
    );
}

export default OverviewPage;