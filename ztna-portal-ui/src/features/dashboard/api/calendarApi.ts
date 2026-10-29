export interface Holiday {
    date: string;
    localName: string;
    name: string;
    countryCode: string;
}

export const calendarApi = {
    // Lấy danh sách ngày lễ của 1 quốc gia trong 1 năm
    getHolidays: async (year: number, countryCode: string = 'VN'): Promise<Holiday[]> => {
        try {
            const res = await fetch(`https://date.nager.at/api/v3/PublicHolidays/${year}/${countryCode}`);
            if (!res.ok) throw new Error('Không thể tải dữ liệu ngày lễ');
            return await res.json();
        } catch (error) {
            console.error(error);
            return [];
        }
    }
};