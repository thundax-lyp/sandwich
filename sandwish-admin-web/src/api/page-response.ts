export interface PageResponse<T> {
    pageNo: number;
    pageSize: number;
    totalPage: number;
    count: number;
    totalCount?: number;
    records: T[];
}
