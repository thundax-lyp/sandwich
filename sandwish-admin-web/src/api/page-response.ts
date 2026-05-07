export interface PageResponse<T> {
    pageNo: number;
    pageSize: number;
    totalPage: number;
    totalCount: number;
    records: T[];
}
