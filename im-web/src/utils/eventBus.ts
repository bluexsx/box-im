import mitt from 'mitt';
import type { GroupVO } from '@/api/group/types';
import type { UserVO } from '@/api/user/types';
import type { ChatMessage } from '@/types';

export type Pos = { x: number; y: number };

export type FullImagePayload =
  | string
  | {
      url: string;
      convKey?: string;
      seqNo?: number;
      localId?: string | number;
    };

export type EventBusEvents = {
  openPrivateVideo: unknown;
  openGroupVideo: void;
  openUserInfo: { user: UserVO; pos: Pos };
  openGroupInfo: { group: GroupVO; pos: Pos };
  openFullImage: FullImagePayload;
  locateChatMessage: ChatMessage;
  newMessage: ChatMessage;
};

const eventBus = mitt<EventBusEvents>();

export default eventBus;
