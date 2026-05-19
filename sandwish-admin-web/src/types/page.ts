export type PageQuery<TQuery extends object = Record<string, never>> = TQuery & {
    pageNo?: number;
    pageSize?: number;
};

export interface Page<TRecord> {
    pageNo: number;
    pageSize: number;
    totalPage: number;
    count: number;
    totalCount?: number;
    records: TRecord[];
}
