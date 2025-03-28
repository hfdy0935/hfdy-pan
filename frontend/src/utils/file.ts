import type { IFileItem, IItemCategory, IItemMediaType, ITreeData } from '@/types/file';

export const isFile = <T extends Record<'mediaType', IItemMediaType>>(item?: T | null) => item?.mediaType !== 'folder'
export const isFolder = <T extends Record<'mediaType', IItemMediaType>>(item?: T | null) => !isFile(item)
export const isVideo = <T extends Record<'category', IItemCategory>>(item?: T | null) => item?.category === 'video'
export const isAudio = <T extends Record<'category', IItemCategory>>(item?: T | null) => item?.category === 'audio'
export const isImage = <T extends Record<'category', IItemCategory>>(item?: T | null) => item?.category === 'image'


export const calcFileLevel = (item: IFileItem | ITreeData) => item?.level?.split('/').length ?? 0